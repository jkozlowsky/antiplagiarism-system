import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import App from './App.tsx'
import "./index.css"
import "./lib/i18n"
import {GoogleOAuthProvider} from '@react-oauth/google';

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <GoogleOAuthProvider clientId="233880181990-t0l5j9tq8et8pbpb9n854vtstcuap5t0.apps.googleusercontent.com">
            <App/>
        </GoogleOAuthProvider>
    </StrictMode>
)