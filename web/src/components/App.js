import React from 'react';
import { Link }  from 'react-router';
import SearchForm from './SearchForm';
import TrackList from './TrackList';
import AppBar from 'material-ui/lib/app-bar';

export default React.createClass({
  getInitialState() {
    return {data: []};
  },
  handleSearch(data) {
    this.setState({data});
  },
  render() {
    return (
      <div>
        <AppBar title='Music Room'/>
        <section>
          <SearchForm onSearchSubmit={this.handleSearch}/>
          <TrackList data={this.state.data} />
        </section>
      </div>
    )
  }
});
