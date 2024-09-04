import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import SideNavbar from './component/SideNavBar';
import CreateTicket from './component/CreateTicket';
import ViewTicket from './component/ViewTicket';
import ViewAllQueue from './component/ViewAllQueue';


function App() {
  return (
    <Router>
      <div className="d-flex">
        <SideNavbar />
        <div className="flex-grow-1 p-3">
          <Routes>
            <Route path="/create-ticket" element={<CreateTicket />} />
            <Route path="/view-ticket" element={<ViewTicket />} />
            <Route path='/viewAll-queues' element={<ViewAllQueue/>}/>
            <Route path="/" element={<h1>Welcome to the Dashboard</h1>} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;

