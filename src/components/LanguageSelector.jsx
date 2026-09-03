```jsx
import React, { useState, useEffect } from 'react';

// This is a pseudo-function representing how the preferences could be stored/retrieved.
import { getStoredLanguage, storeLanguagePreference } from '../services/languageService';

const LanguageSelector = () => {
  const [language, setLanguage] = useState('en');
  const availableLanguages = [
    { code: 'en', name: 'English' },
    { code: 'fr', name: 'Français' },
    { code: 'es', name: 'Español' }
  ];

  useEffect(() => {
    const storedLanguage = getStoredLanguage();
    if (storedLanguage) {
      setLanguage(storedLanguage);
    }
  }, []);

  const handleChange = (event) => {
    const selectedLanguage = event.target.value;
    setLanguage(selectedLanguage);
    storeLanguagePreference(selectedLanguage);
    // Logic to update the application language goes here
  };

  return (
    <div className="language-selector">
      <label htmlFor="language-select">Select Language:</label>
      <select id="language-select" value={language} onChange={handleChange}>
        {availableLanguages.map(lang => (
          <option key={lang.code} value={lang.code}>
            {lang.name}
          </option>
        ))}
      </select>
    </div>
  );
};

export default LanguageSelector;
```

### Integrating LanguageSelector in Navigation or Settings

Next, we will integrate this component into the navigation or settings area of the application. Let's assume there is a `NavBar` component where this can be embedded.