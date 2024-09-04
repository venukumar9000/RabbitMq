import React, { useState } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';
import axios from 'axios';

const TicketModal = ({ show, handleClose, ticket }) => {
  const [formData, setFormData] = useState({
    status: ticket.status,
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.put(`http://localhost:8080/tickets/updateStatus/${ticket.ticketId}`, null, {
        params: {
          status: formData.status,
        },
      });

      console.log('Ticket status updated:', {
        ticketId: ticket.ticketId,
        status: formData.status,
      });

      handleClose(); // Close the modal after saving
    } catch (err) {
      console.error('Error updating ticket status:', err.message);
    }
  };

  return (
    <Modal show={show} onHide={handleClose}>
      <Modal.Header closeButton>
        <Modal.Title>Edit Ticket</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form onSubmit={handleSubmit}>
          <div className="container">
            <div className="row mb-3">
              <div className="col-sm-4">
                <Form.Group controlId="formTicketId">
                  <Form.Label>Ticket ID</Form.Label>
                  <Form.Control type="text" value={ticket.ticketId} readOnly />
                </Form.Group>
              </div>
              <div className="col-sm-4">
                <Form.Group controlId="formTicketType">
                  <Form.Label>Ticket Type</Form.Label>
                  <Form.Control type="text" value={ticket.ticketType} readOnly />
                </Form.Group>
              </div>
              <div className="col-sm-4">
                <Form.Group controlId="formSiteName">
                  <Form.Label>Site Name</Form.Label>
                  <Form.Control type="text" value={ticket.siteName} readOnly />
                </Form.Group>
              </div>
            </div>
            <div className="row mb-3">
              <div className="col-sm-6">
                <Form.Group controlId="formTicketTitle">
                  <Form.Label>Title</Form.Label>
                  <Form.Control type="text" value={ticket.ticketTitle} readOnly />
                </Form.Group>
              </div>
              <div className="col-sm-6">
                <Form.Group controlId="formPriority">
                  <Form.Label>Priority</Form.Label>
                  <Form.Control type="text" value={ticket.priority} readOnly />
                </Form.Group>
              </div>
            </div>
            <div className="row mb-3">
              <div className="col-sm-6">
                <Form.Group controlId="formCreatedTime">
                  <Form.Label>Created Time</Form.Label>
                  <Form.Control type="text" value={new Date(...ticket.createdTime).toLocaleString()} readOnly />
                </Form.Group>
              </div>
              <div className="col-sm-6">
                <Form.Group controlId="formStatus">
                  <Form.Label>Status</Form.Label>
                  <Form.Control
                    as="select"
                    name="status"
                    value={formData.status}
                    onChange={handleChange}
                    className="custom-select"
                  >
                    <option value="new">New</option>
                    <option value="open">Open</option>
                    <option value="inprogress">In Progress</option>
                    <option value="fixed">Fixed</option>
                    <option value="closed">Closed</option>
                  </Form.Control>
                </Form.Group>
              </div>
            </div>
          </div>
          <Button className='ms-3' variant="primary" type="submit">
            Save Changes
          </Button>
        </Form>
      </Modal.Body>
    </Modal>
  );
};

export default TicketModal;
