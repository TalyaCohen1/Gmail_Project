import React, { useState } from "react";
import CreateMail from "./CreateMail";
import { useComposer } from "../context/ComposerContext";

export default function EmailActions({ email, onEmailUpdate }) {
  const { openComposer } = useComposer();
  const [inlineAction, setInlineAction] = useState(null); // "reply" or "forward"

  const isDraft =
    email.send === false ||
    email.labels?.some(label => label.name?.toLowerCase() === "drafts");

  const getQuotedBody = (label) =>
    `\n\n--- ${label} ---\nFrom: ${email.from}\nDate: ${new Date(email.date).toLocaleString()}\nSubject: ${email.subject}\n\n${email.body}`;

  const handleReply = () => setInlineAction("reply");
  const handleForward = () => setInlineAction("forward");

  const handleEditDraft = () => {
    openComposer({ existingEmail: email }); // קופץ
  };

  const getDefaultValues = () => {
    if (inlineAction === "reply") {
      return {
        to: email.from,
        subject: `RE: ${email.subject}`,
        body: getQuotedBody("Original Message")
      };
    }
    if (inlineAction === "forward") {
      return {
        to: "",
        subject: `FWD: ${email.subject}`,
        body: getQuotedBody("Forwarded Message")
      };
    }
    return null;
  };

  const handleInlineDone = (sentEmail) => {
    setInlineAction(null);
    if (sentEmail && onEmailUpdate) onEmailUpdate(sentEmail);
  };

  return (
    <>
      <div className="email-actions">
        {isDraft ? (
          <button onClick={handleEditDraft}>✏️ Edit Draft</button>
        ) : (
          <>
            <button onClick={() => console.log("Star clicked")}>★ Star</button>
            <button onClick={handleReply}>↩️ Reply</button>
            <button onClick={handleForward}>↪️ Forward</button>
          </>
        )}
      </div>

      {inlineAction && (
        <div className="inline-reply">
          <CreateMail
            defaultValues={getDefaultValues()}
            onSend={handleInlineDone}
            onClose={() => setInlineAction(null)}
          />
        </div>
      )}
    </>
  );
}
