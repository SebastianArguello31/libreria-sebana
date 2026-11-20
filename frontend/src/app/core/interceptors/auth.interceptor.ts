import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const esNuestraApi = req.url.startsWith(environment.apiUrl);
  const esLogin = req.url.endsWith('/auth/login');
  const token = esNuestraApi && !esLogin ? auth.getToken() : null;
  const peticion = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && esNuestraApi && !esLogin) {
        auth.logout();
      }
      return throwError(() => error);
    }),
  );
};
