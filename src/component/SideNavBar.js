import React from 'react';
import { Navbar, Nav } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const SideNavbar = () => {
  return (
    <Navbar bg="dark" variant="dark" className="flex-column vh-100">
      <Navbar.Brand as={Link} to="/">IVIS</Navbar.Brand>
      <Nav className="flex-column">
        <Nav.Link as={Link} to="/create-ticket" className='text-nowrap'>Create Ticket</Nav.Link>
        <Nav.Link as={Link} to="/view-ticket" className='text-nowrap'>View Ticket</Nav.Link>
        <Nav.Link as={Link} to="/viewAll-queues" className='text-nowrap'>viewAllQueue</Nav.Link>
      </Nav>
    </Navbar>
  );
}

export default SideNavbar;
