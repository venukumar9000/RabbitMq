// SyncModal.js
import React from 'react';
import { Modal, Button } from 'react-bootstrap';

const SyncModal = ({ show, handleClose }) => {
  return (
    <Modal show={show} onHide={handleClose}>
      <Modal.Header closeButton>
        <Modal.Title>Sync Status</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        Messages purged and synced successfully!
      </Modal.Body>
      <Modal.Footer>
        <Button variant="primary" onClick={handleClose}>
          Close
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default SyncModal;
