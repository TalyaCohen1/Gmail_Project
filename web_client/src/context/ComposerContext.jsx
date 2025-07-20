import React, { createContext, useContext, useState } from "react";
import CreateMail from "../components/CreateMail";

const ComposerContext = createContext();

export function useComposer() {
  return useContext(ComposerContext);
}

export function ComposerProvider({ children }) {
  const [isOpen, setIsOpen] = useState(false);
  const [emailData, setEmailData] = useState(null); // existingEmail, defaultValues, etc.

  const openComposer = (data = null) => {
    setEmailData(data);
    setIsOpen(true);
  };

  const closeComposer = () => {
    setIsOpen(false);
    setEmailData(null);
  };

  return (
    <ComposerContext.Provider value={{ openComposer, closeComposer }}>
      {children}
      {isOpen && (
        <CreateMail
          existingEmail={emailData?.existingEmail}
          defaultValues={emailData?.defaultValues}
          onClose={closeComposer}
          onSend={emailData?.onSend}
        />
      )}
    </ComposerContext.Provider>
  );
}
