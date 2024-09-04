import React, { useEffect, useState } from 'react';
import axios from 'axios';
import TicketModal from './TicketModal';
import ReactPaginate from 'react-paginate';
import '../css/viewTicket.css' // Import the CSS file for styling
import { Button } from 'react-bootstrap';
import SyncModal from './Syncmodal';

const ViewTicket = () => {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [showSyncModal, setSyncModal] = useState(false);  // State for SyncModal
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [ticketsPerPage] = useState(5); // Number of tickets per page
  const [highlightedTicketId, setHighlightedTicketId] = useState(null);
  const [queuesNames, setQueueNames] = useState({});

  useEffect(() => {
    const fetchTickets = async () => {
      try {
        const response = await axios.get('http://localhost:8080/tickets/allTickets');
        setTickets(response.data);
      } catch (err) {
        setError(err.message || 'Failed to fetch tickets');
      } finally {
        setLoading(false);
      }
    };



    fetchTickets();
    fetchQueue();
  }, []);

  const fetchQueue = async () => {
    try {
      const response = await axios.get('http://localhost:8080/rabbitMq/queues');
      setQueueNames(response.data);
      console.log(response.data);

    } catch (err) {
      console.log("Unable to fetch queues");
    }
  };

  const handleEdit = (ticket) => {
    setSelectedTicket(ticket);
    setShowModal(true);
  };

  const handleCloseModal = (updatedTicketId) => {
    setShowModal(false);
    setSelectedTicket(null);
    setHighlightedTicketId(updatedTicketId); // Highlight the edited ticket
    reloadTickets();
  };

  const reloadTickets = async () => {
    try {
      const response = await axios.get('http://localhost:8080/tickets/allTickets');
      setTickets(response.data);
    } catch (err) {
      setError(err.message || 'Failed to fetch tickets');
    }
  };

  // Calculate the current tickets based on pagination
  const indexOfLastTicket = (currentPage + 1) * ticketsPerPage;
  const indexOfFirstTicket = indexOfLastTicket - ticketsPerPage;
  const currentTickets = tickets.slice(indexOfFirstTicket, indexOfLastTicket);

  const handlePageClick = (data) => {
    setCurrentPage(data.selected);
  };

  const handleSync = async () => {
    try {
      const response = await axios.post('http://localhost:8080/tickets/syncMessages');

      if (response.status === 200) {
        setSyncModal(true); // Show the SyncModal on success
        fetchQueue();
      }
    } catch (err) {
      console.log("Unable to sync");
    }
  };

  if (loading) {
    return (
      <div className="text-center">
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return <p className="text-danger">Error: {error}</p>;
  }

  return (
    <>

      <h3>View Tickets</h3>
      {tickets.length === 0 ? (
        <p>No tickets available.</p>
      ) : (
        <div>
          <div className='d-flex justify-content-end me-5'>
            <Button onClick={handleSync}>Sync</Button>
          </div>
          <div className='d-flex py-2'>
            {Object.entries(queuesNames).map(([queueName, count]) => (
              <div key={queueName} className={`rounded queue-item mx-2 p-2 ${queueName.toLowerCase()}`} >
                <span className="queue-name fs-6">{queueName}</span>
                <span className="badge text-dark queue-badge ">{count}</span>
              </div>
            ))}

          </div>
          <table className="table table-sm table-striped table-bordered">
            <thead>
              <tr>
                <th>Ticket Type</th>
                <th>Site Name</th>
                <th>Title</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Queue</th>
                <th>Created Time</th>
                <th>Description</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {currentTickets.map(ticket => (
                <tr
                  key={ticket.ticketId}
                  className={ticket.ticketId === highlightedTicketId ? 'table-success' : ''}
                >
                  <td>{ticket.ticketType}</td>
                  <td>{ticket.siteName}</td>
                  <td>{ticket.ticketTitle}</td>
                  <td>{ticket.priority}</td>
                  <td>{ticket.status}</td>
                  <td>{ticket.queue}</td>
                  <td>{new Date(...ticket.createdTime).toLocaleString()}</td>
                  <td>{ticket.description}</td>
                  <td>
                    <button
                      className="btn btn-primary"
                      onClick={() => handleEdit(ticket)}
                    >
                      Edit
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {/* Pagination */}
          <div className="d-flex justify-content-end">
            <ReactPaginate
              previousLabel={'Previous'}
              nextLabel={'Next'}
              breakLabel={'...'}
              breakClassName={'page-item'}
              breakLinkClassName={'page-link'}
              pageCount={Math.ceil(tickets.length / ticketsPerPage)}
              marginPagesDisplayed={2}
              pageRangeDisplayed={5}
              onPageChange={handlePageClick}
              containerClassName={'pagination custom-pagination'}
              pageClassName={'page-item'}
              pageLinkClassName={'page-link'}
              previousClassName={'page-item'}
              previousLinkClassName={'page-link'}
              nextClassName={'page-item'}
              nextLinkClassName={'page-link'}
              activeClassName={'active'}
            />
          </div>

          {selectedTicket && (
            <TicketModal
              show={showModal}
              handleClose={() => handleCloseModal(selectedTicket.ticketId)}
              ticket={selectedTicket}
              setSelectedTicket={setSelectedTicket}
            />
          )}

          {/* Sync Modal */}
          <SyncModal
            show={showSyncModal}
            handleClose={() => setSyncModal(false)}
          />
        </div>
      )}


    </>

  );
};

export default ViewTicket;
