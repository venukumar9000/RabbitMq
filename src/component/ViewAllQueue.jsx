import axios from "axios";
import React, { useEffect, useState } from "react";
import QueueModal from './QueueModal'; // Import the modal
import '../css/queue.css'
import { Button } from "react-bootstrap";
import SyncModal from "./Syncmodal";

const ViewAllQueue = () => {
  const [queuesNames, setQueueNames] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [showSyncModal,setSyncModal]=useState(false)
  const [selectedQueue, setSelectedQueue] = useState(null);
  const [tickets, setTickets] = useState([]); // State to store fetched tickets


  const fetchQueue = async () => {
    try {
      const response = await axios.get('http://localhost:8080/rabbitMq/queues');
      setQueueNames(response.data);
    } catch (err) {
      setError("Unable to fetch queues");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchQueue();
  }, []);

  const handleQueueClick = (queueName) => {
    const count = queuesNames[queueName];
    if (count > 0) {
    setSelectedQueue(queueName);
    setShowModal(true);
    }
  };

  const handleFetchTickets = async (queueName, count) => {
    try {
      const response = await axios.get(`http://localhost:8080/tickets/${queueName}/consume?count=${count}`);
      setTickets(response.data); // Store the fetched tickets
      setShowModal(false); // Close modal after fetching tickets
    } catch (err) {
      setError("Unable to fetch tickets");
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

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


  return (
    <div>

<div className='d-flex justify-content-end me-2'>
            <Button onClick={handleSync}>Sync</Button>
          </div>

<div className='d-flex py-2'>
            {Object.entries(queuesNames).map(([queueName, count]) => (
              <div key={queueName} className={`rounded queue-item mx-2 p-2 ${queueName.toLowerCase()}`} onClick={() => handleQueueClick(queueName)} >
                <span role="button" className="queue-name fs-6 ">{queueName}</span>
                <span className="badge queue-badge text-dark">{count}</span>
              </div>
            ))}

          </div>
      {/* <div className="queue-summary mb-4 d-flex">
        {Object.entries(queuesNames).map(([queueName, count]) => (
          <div key={queueName} className={`queue-item ${queueName.toLowerCase()}`} >
            <span className="queue-name">{queueName}</span>
            <span className="queue-badge">{count}</span>
          </div>
        ))}
      </div> */}

      {/* Render fetched tickets */}
      {tickets.length > 0 ? (
        <div className="tickets-list">
          <div><span  className="fw-bold">Tickets from Queue: </span><span >{selectedQueue}</span></div>
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
              </tr>
            </thead>
            <tbody>
              {tickets.map(ticket => (
                <tr key={ticket.ticketId}>
                  <td>{ticket.ticketType}</td>
                  <td>{ticket.siteName}</td>
                  <td>{ticket.ticketTitle}</td>
                  <td>{ticket.priority}</td>
                  <td>{ticket.status}</td>
                  <td>{ticket.queue}</td>
                  <td>{new Date(...ticket.createdTime).toLocaleString()}</td>
                  <td>{ticket.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div>No tickets available for {selectedQueue}</div>
      )}
      

      {selectedQueue && queuesNames[selectedQueue] > 0 &&  (
        <QueueModal
          show={showModal}
          handleClose={() => setShowModal(false)}
          queueName={selectedQueue}
          handleFetchTickets={handleFetchTickets}
        />
      )}

<SyncModal
            show={showSyncModal}
            handleClose={() => setSyncModal(false)}
          />
    </div>
  );
};

export default ViewAllQueue;
