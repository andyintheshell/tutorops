import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import Keycloak from 'keycloak-js'
import './index.css'
import App from './App.tsx'

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
})

const root = createRoot(document.getElementById('root')!)

async function bootstrap() {
  try {
    const authenticated = await keycloak.init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      checkLoginIframe: false,
      silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
    })

    root.render(
      <StrictMode>
        <App keycloak={keycloak} initialAuthenticated={authenticated} />
      </StrictMode>,
    )
  } catch {
    root.render(
      <StrictMode>
        <main className="auth-shell">
          <section className="auth-card" role="alert">
            <p className="eyebrow">TutorOps</p>
            <h1>Authentication unavailable</h1>
            <p>Keycloak could not be reached. Check that the local server is running.</p>
          </section>
        </main>
      </StrictMode>,
    )
  }
}

void bootstrap()
