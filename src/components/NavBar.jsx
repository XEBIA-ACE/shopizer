```jsx
import React from 'react';
import LanguageSelector from './LanguageSelector';

const NavBar = () => {
  return (
    <nav className="navbar">
      <h1>Shopizer</h1>
      <LanguageSelector />
    </nav>
  );
};

export default NavBar;
```

### Service to Handle Language Preference

We need a simple mock service to simulate storing and retrieving the user's language preference. This will be a placeholder for the actual integration with a storage mechanism such as local storage or backend API.