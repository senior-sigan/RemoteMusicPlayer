import React from 'react';
import { render } from 'react-dom';
import { Router, Route, hashHistory } from 'react-router';
import App from './components/App';

window.React = React;

render(
  (<Router history={hashHistory}>
    <Route path="/" component={App}>
    </Route>
  </Router>), document.getElementById('content')
);
