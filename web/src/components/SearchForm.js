import React from 'react';
import 'whatwg-fetch';

export default React.createClass({
  getInitialState() {
    return {q: ''};
  },
  handleSearchChange(e) {
    this.setState({q: e.target.value});
  },
  handleSearch(e) {
    e.preventDefault();
    const url = e.target.action;
    const q = this.state.q.trim();
    if (!q) return;
    this.setState({q: ''});
    fetch(`${url}?q=${q}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      },
    }).then(res => res.json()).then(data => {
      if (data.success) {
        this.props.onSearchSubmit(data.data);
      } else {
        throw new Error(data.error);
      }
    });
  },
  render() {
    return (
      <form id='search' action='/api/vk.json' method='get' onSubmit={this.handleSearch}>
        <input
          autoComplete='off'
          autoCorrect='off'
          name='q'
          type='text'
          placeholder='find music'
          value={this.state.q}
          onChange={this.handleSearchChange} />
        <input type='submit' value='Search'></input>
      </form>
    )
  }
});
