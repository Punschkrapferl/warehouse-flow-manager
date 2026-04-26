import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

interface BackendErrorBody {
  message?: string;
  validationErrors?: Record<string, string>;
}

@Injectable({
  providedIn: 'root',
})
export class ApiErrorMessageService {
  toMessage(error: unknown, fallbackMessage: string): string {
    const normalizedFallback = this.removeTrailingPunctuation(fallbackMessage);

    if (error instanceof HttpErrorResponse) {
      return this.fromHttpError(error, normalizedFallback);
    }

    if (error instanceof Error && error.message) {
      return error.message;
    }

    return fallbackMessage;
  }

  private fromHttpError(error: HttpErrorResponse, fallbackMessage: string): string {
    const backendMessage = this.extractBackendMessage(error.error);

    if (backendMessage) {
      return `${fallbackMessage} (${error.status}). ${backendMessage}`;
    }

    if (error.status > 0) {
      return `${fallbackMessage} (${error.status}). ${error.message}`;
    }

    return fallbackMessage;
  }

  private extractBackendMessage(errorBody: unknown): string | null {
    if (!errorBody || typeof errorBody !== 'object') {
      return null;
    }

    const backendError = errorBody as BackendErrorBody;

    if (backendError.validationErrors) {
      const validationMessages = Object.entries(backendError.validationErrors)
        .map(([field, message]) => `${field}: ${message}`)
        .join(', ');

      if (validationMessages.length > 0) {
        return validationMessages;
      }
    }

    return backendError.message ?? null;
  }

  private removeTrailingPunctuation(message: string): string {
    return message.trim().replace(/[.!?]+$/, '');
  }
}
