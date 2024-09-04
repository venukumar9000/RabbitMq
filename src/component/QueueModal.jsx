import React, { useState } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';

const QueueModal = ({ show, handleClose, queueName, handleFetchTickets }) => {
  const [ticketCount, setTicketCount] = useState(1);

  const handleSubmit = () => {
    handleFetchTickets(queueName, ticketCount);
    handleClose();
  };

  return (
    <Modal show={show} onHide={handleClose}>
      <Modal.Header closeButton>
        <Modal.Title>Queue: {queueName}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form>
          <Form.Group controlId="ticketCount">
            <Form.Label>Select Number of Tickets</Form.Label>
            <Form.Control
              as="select"
              value={ticketCount}
              onChange={(e) => setTicketCount(e.target.value)}
            >
              {[1, 2, 3, 4].map((count) => (
                <option key={count} value={count}>
                  {count}
                </option>
              ))}
            </Form.Control>
          </Form.Group>
        </Form>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>
          Close
        </Button>
        <Button variant="primary" onClick={handleSubmit}>
          Fetch Tickets
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default QueueModal;

