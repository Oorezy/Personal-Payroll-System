# IntroTech Angular client

The frontend for the Personal Payroll Management System lives entirely in this folder.

## Development

1. Start Discovery Service, Authentication Service, Introtech Service, and Gateway Service.
2. Install dependencies with `npm install`.
3. Run `npm start`.
4. Open `http://localhost:4200`.

The development proxy sends `/payroll/**` and `/auth/**` requests to the gateway on port `7001`.

## Checks

- Production build: `npm run build`
- Production dependency audit: `npm audit --omit=dev`

Payment providers remain in sandbox/mock mode until production provider credentials and verified webhook endpoints are configured on the backend.
