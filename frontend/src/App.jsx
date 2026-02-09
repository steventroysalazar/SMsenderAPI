import { useMemo, useState } from 'react';

const initialState = {
  deviceNumber: '',
  contactNumber: '',
  smsPassword: '',
  requestLocation: false,
  wifiEnabled: '',
  micVolume: '',
  speakerVolume: '',
  prefixEnabled: '',
  prefixName: '',
  checkBattery: false,
  fallDownEnabled: '',
  fallDownSensitivity: '',
  fallDownCall: '',
  noMotionEnabled: '',
  noMotionTime: '',
  noMotionCall: '',
  apnEnabled: '',
  apn: '',
  serverEnabled: '',
  serverHost: '',
  serverPort: '',
  gprsEnabled: '',
  workingMode: '',
  workingModeInterval: '',
  workingModeNoMotionInterval: '',
  continuousLocateInterval: '',
  continuousLocateDuration: '',
  checkStatus: false
};

const boolSelect = [
  { label: 'Select', value: '' },
  { label: 'On', value: 'true' },
  { label: 'Off', value: 'false' }
];

const callSelect = [
  { label: 'Select', value: '' },
  { label: 'Call: Yes', value: 'true' },
  { label: 'Call: No', value: 'false' }
];

const workingModeOptions = [
  { label: 'Select mode', value: '' },
  { label: 'Mode 1', value: 'mode1' },
  { label: 'Mode 2 (motion interval + no motion interval)', value: 'mode2' },
  { label: 'Mode 3 (interval)', value: 'mode3' },
  { label: 'Mode 4 (interval)', value: 'mode4' },
  { label: 'Mode 5 (interval)', value: 'mode5' },
  { label: 'Mode 6 (motion interval + no motion interval)', value: 'mode6' }
];

const toNullableBoolean = (value) => {
  if (value === 'true') return true;
  if (value === 'false') return false;
  return null;
};

const buildCommandPreview = (state) => {
  const commands = [];
  if (state.contactNumber) {
    commands.push(`A1,1,1,${state.contactNumber}`);
  }
  if (state.smsPassword) {
    commands.push(`P${state.smsPassword}`);
  }
  if (state.requestLocation) {
    commands.push('loc');
  }
  if (state.wifiEnabled !== '') {
    commands.push(`Wifi${state.wifiEnabled === 'true' ? 1 : 0}`);
  }
  if (state.micVolume) {
    commands.push(`Micvolume${state.micVolume}`);
  }
  if (state.speakerVolume) {
    commands.push(`Speakervolume${state.speakerVolume}`);
  }
  if (state.prefixEnabled !== '' && state.prefixName) {
    commands.push(`prefix${state.prefixEnabled === 'true' ? 1 : 0},${state.prefixName}`);
  }
  if (state.checkBattery) {
    commands.push('battery');
  }
  if (state.fallDownEnabled !== '' && state.fallDownSensitivity && state.fallDownCall !== '') {
    commands.push(
      `fl${state.fallDownEnabled === 'true' ? 1 : 0},${state.fallDownSensitivity},${
        state.fallDownCall === 'true' ? 1 : 0
      }`
    );
  }
  if (state.noMotionEnabled !== '' && state.noMotionTime && state.noMotionCall !== '') {
    commands.push(
      `nmo${state.noMotionEnabled === 'true' ? 1 : 0},${state.noMotionTime},${
        state.noMotionCall === 'true' ? 1 : 0
      }`
    );
  }
  if (state.apnEnabled !== '' && state.apn) {
    commands.push(`S${state.apnEnabled === 'true' ? 1 : 0},${state.apn}`);
  }
  if (state.serverEnabled !== '' && state.serverHost && state.serverPort) {
    commands.push(
      `IP${state.serverEnabled === 'true' ? 1 : 0},${state.serverHost},${state.serverPort}`
    );
  }
  if (state.gprsEnabled !== '') {
    commands.push(`S${state.gprsEnabled === 'true' ? 2 : 0}`);
  }
  if (state.workingMode) {
    if (state.workingMode === 'mode1') {
      commands.push('mode1');
    }
    if (state.workingMode === 'mode2' && state.workingModeInterval && state.workingModeNoMotionInterval) {
      commands.push(`mode2,${state.workingModeInterval},${state.workingModeNoMotionInterval}`);
    }
    if (['mode3', 'mode4', 'mode5'].includes(state.workingMode) && state.workingModeInterval) {
      commands.push(`${state.workingMode},${state.workingModeInterval}`);
    }
    if (state.workingMode === 'mode6' && state.workingModeInterval && state.workingModeNoMotionInterval) {
      commands.push(`mode6,${state.workingModeInterval},${state.workingModeNoMotionInterval}`);
    }
  }
  if (state.continuousLocateInterval && state.continuousLocateDuration) {
    commands.push(`CL${state.continuousLocateInterval},${state.continuousLocateDuration}`);
  }
  if (state.checkStatus) {
    commands.push('status');
  }

  return commands.join(';');
};

export default function App() {
  const [formData, setFormData] = useState(initialState);
  const [status, setStatus] = useState({ type: 'idle', message: '' });
  const [messages, setMessages] = useState([]);
  const [inboundMessages, setInboundMessages] = useState([]);
  const [inboundStatus, setInboundStatus] = useState({ type: 'idle', message: '' });

  const apiBase = import.meta.env.VITE_API_BASE || '/api';
  const commandPreview = useMemo(() => buildCommandPreview(formData), [formData]);

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const fetchInboundMessages = async () => {
    setInboundStatus({ type: 'loading', message: 'Loading device replies...' });
    try {
      const response = await fetch(`${apiBase}/inbound-messages`);
      if (!response.ok) {
        throw new Error(`Failed to load replies (${response.status}).`);
      }
      const data = await response.json();
      setInboundMessages(data);
      setInboundStatus({ type: 'success', message: 'Replies updated.' });
    } catch (error) {
      setInboundStatus({ type: 'error', message: error.message });
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setStatus({ type: 'loading', message: 'Sending configuration messages...' });
    setMessages([]);

    const payload = {
      ...formData,
      wifiEnabled: toNullableBoolean(formData.wifiEnabled),
      prefixEnabled: toNullableBoolean(formData.prefixEnabled),
      fallDownEnabled: toNullableBoolean(formData.fallDownEnabled),
      fallDownCall: toNullableBoolean(formData.fallDownCall),
      noMotionEnabled: toNullableBoolean(formData.noMotionEnabled),
      noMotionCall: toNullableBoolean(formData.noMotionCall),
      apnEnabled: toNullableBoolean(formData.apnEnabled),
      serverEnabled: toNullableBoolean(formData.serverEnabled),
      gprsEnabled: toNullableBoolean(formData.gprsEnabled),
      micVolume: formData.micVolume ? Number(formData.micVolume) : null,
      speakerVolume: formData.speakerVolume ? Number(formData.speakerVolume) : null,
      fallDownSensitivity: formData.fallDownSensitivity ? Number(formData.fallDownSensitivity) : null,
      serverPort: formData.serverPort ? Number(formData.serverPort) : null
    };

    try {
      const response = await fetch(`${apiBase}/send-config`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const fallbackMessage = `Request failed with status ${response.status}.`;
        const errorText = await response.text();
        let message = fallbackMessage;

        if (errorText) {
          try {
            const errorPayload = JSON.parse(errorText);
            message =
              errorPayload.message ||
              errorPayload.error ||
              errorPayload.detail ||
              fallbackMessage;
          } catch {
            message = errorText;
          }
        }

        throw new Error(message);
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
            Fill out the command fields below to generate a combined configuration SMS.
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

        <div className="message-list">
          <div className="message-header">
            <h2>Device replies</h2>
            <button type="button" className="secondary" onClick={fetchInboundMessages}>
              Refresh replies
            </button>
          </div>
          {inboundStatus.type !== 'idle' && (
            <p className={`status ${inboundStatus.type}`}>{inboundStatus.message}</p>
          )}
          {inboundMessages.length === 0 ? (
            <p className="empty-state">No replies yet. After sending commands, refresh to check.</p>
          ) : (
            <ul>
              {inboundMessages.map((message, index) => (
                <li key={`${message.receivedAt}-${index}`}>
                  <strong>{message.from || 'Unknown'}:</strong> {message.text}
                </li>
              ))}
            </ul>
          )}
        </div>

        <form onSubmit={handleSubmit} className="form">
          <section className="section">
            <h2>Core setup</h2>
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
            </label>
            <label className="field">
              <span>Set contact number (A1,1,1,phone)</span>
              <input
                type="tel"
                name="contactNumber"
                value={formData.contactNumber}
                onChange={handleChange}
                placeholder="+15551234567"
              />
            </label>
            <label className="field">
              <span>Set SMS password (Ppassword)</span>
              <input
                type="text"
                name="smsPassword"
                value={formData.smsPassword}
                onChange={handleChange}
                placeholder="123456"
                maxLength={16}
              />
            </label>
            <label className="checkbox">
              <input
                type="checkbox"
                name="requestLocation"
                checked={formData.requestLocation}
                onChange={handleChange}
              />
              Request location (loc)
            </label>
          </section>

          <section className="section">
            <h2>Connectivity</h2>
            <div className="grid">
              <label className="field">
                <span>Wi-Fi on/off (Wifi0/1)</span>
                <select name="wifiEnabled" value={formData.wifiEnabled} onChange={handleChange}>
                  {boolSelect.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>APN enable (S0/1,apn)</span>
                <select name="apnEnabled" value={formData.apnEnabled} onChange={handleChange}>
                  {boolSelect.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>APN value</span>
                <input
                  type="text"
                  name="apn"
                  value={formData.apn}
                  onChange={handleChange}
                  placeholder="internet"
                />
              </label>
              <label className="field">
                <span>Server enable (IP0/1,host,port)</span>
                <select name="serverEnabled" value={formData.serverEnabled} onChange={handleChange}>
                  {boolSelect.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Server host</span>
                <input
                  type="text"
                  name="serverHost"
                  value={formData.serverHost}
                  onChange={handleChange}
                  placeholder="www.smart-locator.com"
                />
              </label>
              <label className="field">
                <span>Server port</span>
                <input
                  type="number"
                  name="serverPort"
                  value={formData.serverPort}
                  onChange={handleChange}
                  placeholder="6060"
                />
              </label>
              <label className="field">
                <span>GPRS (S0 off / S2 on)</span>
                <select name="gprsEnabled" value={formData.gprsEnabled} onChange={handleChange}>
                  {boolSelect.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
            </div>
          </section>

          <section className="section">
            <h2>Audio + prefix</h2>
            <div className="grid">
              <label className="field">
                <span>Microphone volume (Micvolume)</span>
                <input
                  type="number"
                  name="micVolume"
                  value={formData.micVolume}
                  onChange={handleChange}
                  placeholder="10"
                />
              </label>
              <label className="field">
                <span>Speaker volume (Speakervolume)</span>
                <input
                  type="number"
                  name="speakerVolume"
                  value={formData.speakerVolume}
                  onChange={handleChange}
                  placeholder="90"
                />
              </label>
              <label className="field">
                <span>Prefix enable (prefix0/1,name)</span>
                <select name="prefixEnabled" value={formData.prefixEnabled} onChange={handleChange}>
                  {boolSelect.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Prefix name</span>
                <input
                  type="text"
                  name="prefixName"
                  value={formData.prefixName}
                  onChange={handleChange}
                  placeholder="Emma"
                />
              </label>
            </div>
          </section>

          <section className="section">
            <h2>Alarms</h2>
            <label className="checkbox">
              <input
                type="checkbox"
                name="checkBattery"
                checked={formData.checkBattery}
                onChange={handleChange}
              />
              Check battery status (battery)
            </label>
            <div className="grid">
              <label className="field">
                <span>Fall down enable (fl0/1,sensitivity,call)</span>
                <select name="fallDownEnabled" value={formData.fallDownEnabled} onChange={handleChange}>
                  {boolSelect.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Fall down sensitivity (1-9)</span>
                <input
                  type="number"
                  name="fallDownSensitivity"
                  value={formData.fallDownSensitivity}
                  onChange={handleChange}
                  placeholder="5"
                />
              </label>
              <label className="field">
                <span>Fall down call</span>
                <select name="fallDownCall" value={formData.fallDownCall} onChange={handleChange}>
                  {callSelect.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>No motion enable (nmo0/1,time,call)</span>
                <select name="noMotionEnabled" value={formData.noMotionEnabled} onChange={handleChange}>
                  {boolSelect.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>No motion time (e.g. 80M)</span>
                <input
                  type="text"
                  name="noMotionTime"
                  value={formData.noMotionTime}
                  onChange={handleChange}
                  placeholder="80M"
                />
              </label>
              <label className="field">
                <span>No motion call</span>
                <select name="noMotionCall" value={formData.noMotionCall} onChange={handleChange}>
                  {callSelect.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
            </div>
          </section>

          <section className="section">
            <h2>Working mode</h2>
            <div className="grid">
              <label className="field">
                <span>Mode</span>
                <select name="workingMode" value={formData.workingMode} onChange={handleChange}>
                  {workingModeOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Interval (e.g. 03M, 01H)</span>
                <input
                  type="text"
                  name="workingModeInterval"
                  value={formData.workingModeInterval}
                  onChange={handleChange}
                  placeholder="03M"
                />
              </label>
              <label className="field">
                <span>No motion interval (mode2/mode6)</span>
                <input
                  type="text"
                  name="workingModeNoMotionInterval"
                  value={formData.workingModeNoMotionInterval}
                  onChange={handleChange}
                  placeholder="01H"
                />
              </label>
            </div>
          </section>

          <section className="section">
            <h2>Continuous locate</h2>
            <div className="grid">
              <label className="field">
                <span>Interval (e.g. 10s)</span>
                <input
                  type="text"
                  name="continuousLocateInterval"
                  value={formData.continuousLocateInterval}
                  onChange={handleChange}
                  placeholder="10s"
                />
              </label>
              <label className="field">
                <span>Duration (e.g. 600s)</span>
                <input
                  type="text"
                  name="continuousLocateDuration"
                  value={formData.continuousLocateDuration}
                  onChange={handleChange}
                  placeholder="600s"
                />
              </label>
            </div>
          </section>

          <section className="section">
            <h2>Diagnostics</h2>
            <label className="checkbox">
              <input
                type="checkbox"
                name="checkStatus"
                checked={formData.checkStatus}
                onChange={handleChange}
              />
              Check function settings (status)
            </label>
          </section>

          <section className="section">
            <h2>Command preview</h2>
            <p className="hint">Combined SMS payload (semicolon-separated).</p>
            <textarea
              className="command-preview"
              value={commandPreview}
              readOnly
              rows={3}
            />
            <p className="hint">
              Length: {commandPreview.length} characters. If over 150, it will be sent as two SMS
              messages (150 + remainder).
            </p>
          </section>

          <button type="submit" disabled={status.type === 'loading'}>
            {status.type === 'loading' ? 'Sending...' : 'Send configuration SMS'}
          </button>
        </form>
      </main>
    </div>
  );
}
