package se.sundsvall.esigning.integration.engine.externaltask;

import java.util.Map;
import org.camunda.bpm.client.exception.EngineException;
import org.camunda.bpm.client.exception.RestException;
import org.camunda.bpm.client.exception.UnknownHttpErrorException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class UnauthorizedRetryingExternalTaskServiceTest {

	private static final Map<String, Object> VARIABLES = Map.of("key", "value");

	@Mock
	private ExternalTaskService externalTaskServiceMock;

	@Mock
	private OperatonExternalTaskToken tokenMock;

	@Mock
	private ExternalTask externalTaskMock;

	@Test
	void successfulCallIsMadeOnceWithoutEviction() {
		UnauthorizedRetryingExternalTaskService.wrap(externalTaskServiceMock, tokenMock).complete(externalTaskMock, VARIABLES);

		verify(externalTaskServiceMock).complete(externalTaskMock, VARIABLES);
		verifyNoMoreInteractions(externalTaskServiceMock);
		verifyNoInteractions(tokenMock);
	}

	@Test
	void unauthorizedThenSuccessEvictsTokenAndRetriesExactlyOnce() {
		doThrow(unauthorized()).doNothing().when(externalTaskServiceMock).complete(externalTaskMock, VARIABLES);

		UnauthorizedRetryingExternalTaskService.wrap(externalTaskServiceMock, tokenMock).complete(externalTaskMock, VARIABLES);

		final var inOrder = inOrder(externalTaskServiceMock, tokenMock);
		inOrder.verify(externalTaskServiceMock).complete(externalTaskMock, VARIABLES);
		inOrder.verify(tokenMock).evict();
		inOrder.verify(externalTaskServiceMock).complete(externalTaskMock, VARIABLES);
		verifyNoMoreInteractions(externalTaskServiceMock, tokenMock);
	}

	@Test
	void unauthorizedTwiceEvictsRetriesOnceAndRethrows() {
		final var secondFailure = unauthorized();
		doThrow(unauthorized()).doThrow(secondFailure).when(externalTaskServiceMock).handleFailure("id", "message", null, 2, 10L);

		final var externalTaskService = UnauthorizedRetryingExternalTaskService.wrap(externalTaskServiceMock, tokenMock);

		assertThatExceptionOfType(UnknownHttpErrorException.class)
			.isThrownBy(() -> externalTaskService.handleFailure("id", "message", null, 2, 10L))
			.isSameAs(secondFailure);

		verify(externalTaskServiceMock, times(2)).handleFailure("id", "message", null, 2, 10L);
		verify(tokenMock).evict();
		verifyNoMoreInteractions(externalTaskServiceMock, tokenMock);
	}

	@Test
	void serverErrorIsRethrownWithoutRetryOrEviction() {
		final var failure = new EngineException("Internal error", restException(500));
		doThrow(failure).when(externalTaskServiceMock).complete(externalTaskMock);

		final var externalTaskService = UnauthorizedRetryingExternalTaskService.wrap(externalTaskServiceMock, tokenMock);

		assertThatExceptionOfType(EngineException.class)
			.isThrownBy(() -> externalTaskService.complete(externalTaskMock))
			.isSameAs(failure);

		verify(externalTaskServiceMock).complete(externalTaskMock);
		verifyNoMoreInteractions(externalTaskServiceMock);
		verifyNoInteractions(tokenMock);
	}

	@Test
	void errorIsRethrownWithoutRetryOrEviction() {
		doThrow(new OutOfMemoryError("out of memory")).when(externalTaskServiceMock).complete(externalTaskMock);

		final var externalTaskService = UnauthorizedRetryingExternalTaskService.wrap(externalTaskServiceMock, tokenMock);

		assertThatExceptionOfType(OutOfMemoryError.class)
			.isThrownBy(() -> externalTaskService.complete(externalTaskMock));

		verify(externalTaskServiceMock).complete(externalTaskMock);
		verifyNoInteractions(tokenMock);
	}

	@Test
	void unauthorizedOnExtendLockEvictsTokenAndRetriesOnce() {
		doThrow(unauthorized()).doNothing().when(externalTaskServiceMock).extendLock(externalTaskMock, 1000L);

		UnauthorizedRetryingExternalTaskService.wrap(externalTaskServiceMock, tokenMock).extendLock(externalTaskMock, 1000L);

		verify(externalTaskServiceMock, times(2)).extendLock(externalTaskMock, 1000L);
		verify(tokenMock).evict();
		verifyNoMoreInteractions(externalTaskServiceMock, tokenMock);
	}

	@Test
	void unauthorizedOnHandleBpmnErrorEvictsTokenAndRetriesOnce() {
		doThrow(unauthorized()).doNothing().when(externalTaskServiceMock).handleBpmnError(externalTaskMock, "code", "message", VARIABLES);

		UnauthorizedRetryingExternalTaskService.wrap(externalTaskServiceMock, tokenMock).handleBpmnError(externalTaskMock, "code", "message", VARIABLES);

		verify(externalTaskServiceMock, times(2)).handleBpmnError(externalTaskMock, "code", "message", VARIABLES);
		verify(tokenMock).evict();
		verifyNoMoreInteractions(externalTaskServiceMock, tokenMock);
	}

	private static UnknownHttpErrorException unauthorized() {
		return new UnknownHttpErrorException("Unauthorized", restException(401));
	}

	private static RestException restException(final int httpStatusCode) {
		final var restException = new RestException("message", "type", 0);
		restException.setHttpStatusCode(httpStatusCode);
		return restException;
	}
}
