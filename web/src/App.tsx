import { useCallback, useEffect, useState } from 'react'
import type { KeycloakInstance } from 'keycloak-js'
import './App.css'

type AppProps = {
  keycloak: KeycloakInstance
  initialAuthenticated: boolean
}

const roleEndpoints = [
  { role: 'STUDENT', path: '/api/student/ping' },
  { role: 'TUTOR', path: '/api/tutor/ping' },
  { role: 'ADMIN', path: '/api/admin/ping' },
] as const

type EndpointResult = {
  path: string
  role: string
  status: number | 'error' | null
}

type DecodedToken = {
  header: unknown
  payload: unknown
  error?: string
}

type TokenDetails = {
  accessToken: DecodedToken
  idToken: DecodedToken
}

function decodeToken(token: string | undefined): DecodedToken {
  if (!token) return { header: null, payload: null, error: 'Token is not available.' }

  try {
    const [encodedHeader, encodedPayload] = token.split('.')
    if (!encodedHeader || !encodedPayload) throw new Error('Invalid JWT format.')

    const decodePart = (part: string) => {
      const base64 = part.replace(/-/g, '+').replace(/_/g, '/')
      const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
      return JSON.parse(new TextDecoder().decode(Uint8Array.from(atob(padded), (character) => character.charCodeAt(0))))
    }

    return { header: decodePart(encodedHeader), payload: decodePart(encodedPayload) }
  } catch {
    return { header: null, payload: null, error: 'Unable to decode this token.' }
  }
}

function TokenSection({ title, token }: { title: string; token: DecodedToken }) {
  return (
    <section className="token-section">
      <h3>{title}</h3>
      {token.error ? <p className="token-error">{token.error}</p> : (
        <>
          <h4>Header</h4>
          <pre>{JSON.stringify(token.header, null, 2)}</pre>
          <h4>Claims</h4>
          <pre>{JSON.stringify(token.payload, null, 2)}</pre>
        </>
      )}
    </section>
  )
}

function App({ keycloak, initialAuthenticated }: AppProps) {
  const [authenticated, setAuthenticated] = useState(initialAuthenticated)
  const [endpointResults, setEndpointResults] = useState<EndpointResult[]>(
    roleEndpoints.map((endpoint) => ({ ...endpoint, status: null })),
  )
  const [checkingEndpoints, setCheckingEndpoints] = useState(false)
  const [tokenDetails, setTokenDetails] = useState<TokenDetails | null>(null)
  const [showTokenDialog, setShowTokenDialog] = useState(false)

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

  const inspectTokens = async () => {
    try {
      await keycloak.updateToken(30)
    } catch {
      // Show the current token if refresh is unavailable; decoding is local and read-only.
    }
    setTokenDetails({
      accessToken: decodeToken(keycloak.token),
      idToken: decodeToken(keycloak.idToken),
    })
    setShowTokenDialog(true)
  }

  const checkRoleEndpoints = useCallback(async () => {
    setCheckingEndpoints(true)
    setEndpointResults(roleEndpoints.map((endpoint) => ({ ...endpoint, status: null })))

    try {
      await keycloak.updateToken(30)
      const apiBaseUrl = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')
      const results = await Promise.all(roleEndpoints.map(async (endpoint) => {
        try {
          const response = await fetch(`${apiBaseUrl}${endpoint.path}`, {
            headers: { Authorization: `Bearer ${keycloak.token}` },
          })
          return { ...endpoint, status: response.status }
        } catch {
          return { ...endpoint, status: 'error' as const }
        }
      }))
      setEndpointResults(results)
    } catch {
      setEndpointResults(roleEndpoints.map((endpoint) => ({ ...endpoint, status: 'error' as const })))
    } finally {
      setCheckingEndpoints(false)
    }
  }, [keycloak])

  useEffect(() => {
    if (authenticated) void checkRoleEndpoints()
  }, [authenticated, checkRoleEndpoints])

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
            <button type="button" className="check-button token-button" onClick={() => void inspectTokens()}>
              Inspect tokens
            </button>
            <section className="endpoint-checks" aria-labelledby="endpoint-checks-title">
              <div className="section-heading">
                <div>
                  <p className="section-kicker">Authorization probe</p>
                  <h2 id="endpoint-checks-title">Role endpoint checks</h2>
                </div>
                <button
                  type="button"
                  className="check-button"
                  onClick={() => void checkRoleEndpoints()}
                  disabled={checkingEndpoints}
                >
                  {checkingEndpoints ? 'Checking…' : 'Run checks'}
                </button>
              </div>
              <p className="section-description">
                Each request uses your current Keycloak token. A 200 means the role is allowed; 403 means it is denied.
              </p>
              <div className="endpoint-list">
                {endpointResults.map((endpoint) => (
                  <div className="endpoint-row" key={endpoint.path}>
                    <div>
                      <strong>{endpoint.role}</strong>
                      <code>{endpoint.path}</code>
                    </div>
                    <span className={`endpoint-status status-${endpoint.status ?? 'pending'}`}>
                      {endpoint.status === null ? '—' : endpoint.status === 'error' ? 'ERROR' : endpoint.status}
                    </span>
                  </div>
                ))}
              </div>
            </section>
            <button type="button" className="button secondary" onClick={logout}>Sign out</button>
          </>
        ) : (
          <>
            <p>Sign in to access your TutorOps workspace.</p>
            <button type="button" className="button" onClick={login}>Sign in</button>
          </>
        )}
      </section>
      {showTokenDialog && tokenDetails && (
        <div className="token-dialog-backdrop" role="presentation" onClick={() => setShowTokenDialog(false)}>
          <section
            className="token-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="token-dialog-title"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="section-heading">
              <div>
                <p className="section-kicker">Local inspection</p>
                <h2 id="token-dialog-title">Decoded tokens</h2>
              </div>
              <button type="button" className="dialog-close" onClick={() => setShowTokenDialog(false)} aria-label="Close token dialog">×</button>
            </div>
            <p className="token-warning">These claims are sensitive. They were decoded in your browser and are not sent anywhere by this view.</p>
            <TokenSection title="Authorization token (access token)" token={tokenDetails.accessToken} />
            <TokenSection title="ID token" token={tokenDetails.idToken} />
          </section>
        </div>
      )}
    </main>
  )
}

export default App
