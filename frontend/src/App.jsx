import { useState } from 'react';

const initialState = {
  deviceNumber: '',
  patientPhone: '',
  alertPhone: '',
  heartbeatInterval: '',
  apn: '',
  serverUrl: ''
};

const fieldHints = [
  { name: 'patientPhone', label: 'Patient phone number', hint: 'PTPHONE:<number>' },
  { name: 'alertPhone', label: 'Alert/clinician phone', hint: 'ALERT:<number>' },
  { name: 'heartbeatInterval', label: 'Heartbeat interval (minutes)', hint: 'HEART:<minutes>', type: 'number' },
  { name: 'apn', label: 'APN', hint: 'APN:<apn>' },
  { name: 'serverUrl', label: 'Server URL', hint: 'SERVER:<url>', type: 'url' }
];

export default function App() {
  const [formData, setFormData] = useState(initialState);
  const [status, setStatus] = useState({ type: 'idle', message: '' });
  const [messages, setMessages] = useState([]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setStatus({ type: 'loading', message: 'Sending configuration messages...' });
    setMessages([]);

    const payload = {
      ...formData,
      heartbeatInterval: formData.heartbeatInterval
        ? Number(formData.heartbeatInterval)
        : null
    };

    try {
      const response = await fetch('/api/send-config', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorPayload = await response.json().catch(() => ({}));
        throw new Error(errorPayload.message || 'Failed to send configuration.');
      }

      const data = await response.json();
      setMessages(data.messages || []);
      setStatus({
        type: 'success',
        message: `Sent ${data.messages?.length || 0} messages to ${data.deviceNumber}.`
      });
    } catch (error) {
      setStatus({ type: 'error', message: error.message });
    }
  };

  return (
    <div className="page">
      <main className="card">
        <header>
          <p className="eyebrow">EV12 Remote Patient Monitoring</p>
          <h1>Configure device via SMS</h1>
          <p className="subtitle">
            Complete the form to generate and send the SMS commands required to configure
            the EV12 SOS button.
          </p>
        </header>

        {status.type !== 'idle' && (
          <div className={`alert ${status.type}`}>
            {status.message}
          </div>
        )}

        {messages.length > 0 && (
          <div className="message-list">
            <h2>Messages sent</h2>
            <ul>
              {messages.map((message) => (
                <li key={message.body}>{message.body}</li>
              ))}
            </ul>
          </div>
        )}

        <form onSubmit={handleSubmit} className="form">
          <label className="field">
            <span>Device phone number</span>
            <input
              type="tel"
              name="deviceNumber"
              value={formData.deviceNumber}
              onChange={handleChange}
              placeholder="+1 555-555-5555"
              required
            />
            <small>The SIM number inside the EV12 device.</small>
          </label>

          <div className="grid">
            {fieldHints.map((field) => (
              <label className="field" key={field.name}>
                <span>{field.label}</span>
                <input
                  type={field.type || 'text'}
                  name={field.name}
                  value={formData[field.name]}
                  onChange={handleChange}
                  placeholder={field.hint}
                />
                <small>SMS command: {field.hint}</small>
              </label>
            ))}
          </div>

          <button type="submit" disabled={status.type === 'loading'}>
            {status.type === 'loading' ? 'Sending...' : 'Send configuration SMS'}
          </button>
        </form>
      </main>
    </div>
  );
}
