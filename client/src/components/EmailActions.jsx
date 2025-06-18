import React, { useState, useEffect } from "react";
import CreateMail from "./CreateMail";
import { useComposer } from "../context/ComposerContext";
import { markEmailAsStarred } from "../services/mailService";
import "../styles/EmailDetail.css";

export default function EmailActions({ email, onEmailUpdate, onRefresh }) {
  const { openComposer } = useComposer();
  const [inlineAction, setInlineAction] = useState(null); // "reply" or "forward"
  const [isStarred, setIsStarred] = useState(email.starred || false);

  const isDraft =
    email.send === false ||
    email.labels?.some(label => label.name?.toLowerCase() === "drafts");

  const getQuotedBody = (label) =>
    `\n\n--- ${label} ---\nFrom: ${email.from}\nDate: ${new Date(email.date).toLocaleString()}\nSubject: ${email.subject}\n\n${email.body}`;

  const handleReply = () => setInlineAction("reply");
  const handleForward = () => setInlineAction("forward");

  const handleEditDraft = () => {
    openComposer({ existingEmail: email }); 
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

  const handleToggleStar = async () => {
    try {
      await markEmailAsStarred(email.id);
      setIsStarred(prev => !prev);
    } catch (err) {
      console.error("Failed to star email", err);
    }
  };

  // Scroll to the inline reply section when it appears
  useEffect(() => {
    if (inlineAction) {
      setTimeout(() => {
        document.querySelector('.inline-reply')?.scrollIntoView({ behavior: 'smooth' });
      }, 100); 
    }
  }, [inlineAction]);


  return (
    <>
    <div className="email-star-top">
      <button onClick={handleToggleStar} className={`star-btn ${isStarred ? 'starred' : ''}`}>
        {isStarred ? '★' : '☆'}
      </button>
    </div>
      <div className="email-actions">
        {isDraft ? (
          <button onClick={handleEditDraft}>✏️ Edit Draft</button>
        ) : (
          <>
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
            onRefresh={onRefresh}
          />
        </div>
      )}
    </>
  );
}
