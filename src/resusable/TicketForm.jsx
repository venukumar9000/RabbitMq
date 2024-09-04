import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { v4 as uuidv4 } from 'uuid';
import moment from 'moment';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const TicketForm = () => {
  const [ticket, setTicket] = useState({
     // Auto-generate ticketId
    ticketType: '',
    siteName: '',
    ticketTitle: '',
    priority: '',
    status: 'new', // Default status
    // createdTime: moment().format('YYYY-MM-DD HH:mm:ss'), // Auto-generate created time
    queue: '',
    description: '',
   // Set default value to true
  });

  const ticketTitles = {
    Maintenance: [
      'Camera Disconnected',
      'Change Camera Position',
      'Site Down',
      'Site Relocation',
      'Client Request',
      'Hard Disk Problem',
      'HDD Not Recording'
    ],
    'Site Installation': ['Site Installation'],
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setTicket((prevTicket) => ({
      ...prevTicket,
      [name]: value,
    }));
  };

  const handleTicketTypeChange = (e) => {
    const { value } = e.target;
    setTicket((prevTicket) => ({
      ...prevTicket,
      ticketType: value,
      ticketTitle: '', // Reset ticket title when ticket type changes
    }));
  };


  const handleSubmit = async (e) => {
    e.preventDefault();

    console.log('Submitting ticket:', ticket);

    try {
        
        
      const response = await axios.post('http://localhost:8080/tickets/create', ticket);
      
      console.log('Ticket Created:', response.data);

      // Reset form fields while keeping default values
      setTicket({
        // ticketId: uuidv4(),
        ticketType: '',
        siteName: '',
        ticketTitle: '',
        priority: '',
        status: 'new',
        // createdTime: moment().format('YYYY-MM-DD HH:mm:ss'),
        queue: '',
        description: '',
      });
      toast("Ticket Successfully Created")

    } catch (error) {
      console.error('There was an error creating the ticket!', error);
      toast('Failed to create ticket. Please try again.');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="row mb-3">
        <div className="col">
          <label htmlFor="ticketType" className="form-label">Ticket Type</label>
          <select
            id="ticketType"
            name="ticketType"
            className="form-select"
            value={ticket.ticketType}
            onChange={handleTicketTypeChange}
            required
          >
            <option value="">Select Ticket Type</option>
            <option value="Maintenance">Maintenance</option>
            <option value="Site Installation">Site Installation</option>
          </select>
        </div>

        <div className="col">
          <label htmlFor="ticketTitle" className="form-label">Title</label>
          <select
            id="ticketTitle"
            name="ticketTitle"
            className="form-select"
            value={ticket.ticketTitle}
            onChange={handleChange}
            required
            disabled={!ticket.ticketType} // Disable if ticket type is not selected
          >
            <option value="">Select Title</option>
            {ticket.ticketType && ticketTitles[ticket.ticketType].map((title, index) => (
              <option key={index} value={title}>{title}</option>
            ))}
          </select>
        </div>

        <div className="col">
          <label htmlFor="priority" className="form-label">Priority</label>
          <select
            id="priority"
            name="priority"
            className="form-select"
            value={ticket.priority}
            onChange={handleChange}
            required
          >
            <option value="">Select Priority</option>
            <option value="High">High</option>
            <option value="Medium">Medium</option>
            <option value="Low">Low</option>
          </select>
        </div>
      </div>
      <ToastContainer />
      <div className="row mb-3">
        <div className="col">
          <label htmlFor="siteName" className="form-label">Site Name</label>
          <input
            type="text"
            id="siteName"
            name="siteName"
            className="form-control"
            value={ticket.siteName}
            onChange={handleChange}
            required
          />
        </div>
      </div>

      <div className="row mb-3">
        <div className="col">
          <label htmlFor="description" className="form-label">Description</label>
          <textarea
            id="description"
            name="description"
            className="form-control"
            value={ticket.description}
            onChange={handleChange}
            rows="3"
            required
          ></textarea>
        </div>
      </div>

      <button type="submit" className="btn btn-primary">Submit</button>
    </form>
  );
};

export default TicketForm;