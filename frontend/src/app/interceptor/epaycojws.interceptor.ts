import { HttpInterceptorFn } from "@angular/common/http";

export const epaycoJwsInterceptor: HttpInterceptorFn = (req, next) => {
    const authToken = localStorage.getItem('epayco_jwt_token');

    if (authToken) {
        const authReq = req.clone({
            headers: req.headers.set('Authorization', `Bearer ${authToken}`)
        });

        return next(authReq);
    }

    return next(req);
};