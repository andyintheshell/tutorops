import { useEffect, useState } from 'react'
import type { KeycloakInstance } from 'keycloak-js'
import './App.css'

type AppProps = {
  keycloak: KeycloakInstance
  initialAuthenticated: boolean
}

function App({ keycloak, initialAuthenticated }: AppProps) {
  const [authenticated, setAuthenticated] = useState(initialAuthenticated)

  useEffect(() => {
    keycloak.onAuthSuccess = () => setAuthenticated(true)
    keycloak.onAuthRefreshSuccess = () => setAuthenticated(true)
    keycloak.onAuthLogout = () => setAuthenticated(false)
    keycloak.onTokenExpired = () => {
      void keycloak.updateToken(30).catch(() => {
        setAuthenticated(false)
        keycloak.clearToken()
      })
    }

    return () => {
      keycloak.onAuthSuccess = undefined
      keycloak.onAuthRefreshSuccess = undefined
      keycloak.onAuthLogout = undefined
      keycloak.onTokenExpired = undefined
    }
  }, [keycloak])

  const username = keycloak.tokenParsed?.preferred_username
  const apiClientId = import.meta.env.VITE_API_CLIENT_ID || 'tutorops-api'
  const roles = Array.from(new Set([
    ...(keycloak.resourceAccess?.[apiClientId]?.roles ?? []),
    ...(keycloak.realmAccess?.roles ?? []),
  ])).sort()

  const login = () => void keycloak.login()
  const logout = () => void keycloak.logout({ redirectUri: window.location.origin })

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <p className="eyebrow">TutorOps</p>
        <h1>{authenticated ? `Welcome${username ? `, ${username}` : ''}` : 'Welcome to TutorOps'}</h1>
        {authenticated ? (
          <>
            <p className="status"><span className="status-dot" />You are signed in.</p>
            {roles.length > 0 ? (
              <div className="roles" aria-label="Your roles">
                {roles.map((role) => <span key={role}>{role}</span>)}
              </div>
            ) : <p className="role-empty">No application roles were included in your token.</p>}
            <button type="button" className="button secondary" onClick={logout}>Sign out</button>
          </>
        ) : (
          <>
            <p>Sign in to access your TutorOps workspace.</p>
            <button type="button" className="button" onClick={login}>Sign in</button>
          </>
        )}
      </section>
    </main>
  )
}

export default App
