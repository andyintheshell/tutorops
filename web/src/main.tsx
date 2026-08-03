import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import Keycloak from 'keycloak-js'
import './index.css'
import App from './App.tsx'

const root = createRoot(document.getElementById('root')!)

function requiredEnv(name: string, value: string | undefined) {
  if (!value?.trim()) {
    throw new Error(`Missing required environment variable: ${name}`)
  }

  return value
}

async function bootstrap() {
  try {
    const keycloak = new Keycloak({
      url: requiredEnv('VITE_KEYCLOAK_URL', import.meta.env.VITE_KEYCLOAK_URL),
      realm: requiredEnv('VITE_KEYCLOAK_REALM', import.meta.env.VITE_KEYCLOAK_REALM),
      clientId: requiredEnv('VITE_KEYCLOAK_CLIENT_ID', import.meta.env.VITE_KEYCLOAK_CLIENT_ID),
    })

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
  } catch (error) {
    console.error('Keycloak initialization failed', error)

    const message = error instanceof Error ? error.message : 'Unknown initialization error'
    root.render(
      <StrictMode>
        <main className="auth-shell">
          <section className="auth-card" role="alert">
            <p className="eyebrow">TutorOps</p>
            <h1>Authentication unavailable</h1>
            <p>{message.startsWith('Missing required environment variable:')
              ? message
              : 'Keycloak could not be reached. Check that the local server is running.'}</p>
          </section>
        </main>
      </StrictMode>,
    )
  }
}

void bootstrap()
