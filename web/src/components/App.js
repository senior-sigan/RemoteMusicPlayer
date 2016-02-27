import React from 'react';
import { Link }  from 'react-router';
import SearchForm from './SearchForm';
import TrackList from './TrackList';

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
        <header>
          <h1>Music room</h1>
        </header>
        <section>
          <SearchForm onSearchSubmit={this.handleSearch}/>
          <TrackList data={this.state.data} />
        </section>
      </div>
    )
  }
});
