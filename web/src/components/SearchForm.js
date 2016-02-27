import React from 'react';
import 'whatwg-fetch';
import TextField from 'material-ui/lib/text-field';
import IconButton from 'material-ui/lib/icon-button';
import SearchIcon from 'material-ui/lib/svg-icons/action/search';

export default React.createClass({
  getInitialState() {
    return {q: ''};
  },
  handleSearchChange(e) {
    this.setState({q: e.target.value});
  },
  handleSearch() {
    const q = this.state.q.trim();
    if (!q) {
      this.props.onSearchSubmit([]);
      return;
    }
    const url = `/api/vk.json?q=${q}`;
    fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      },
    }).then(res => res.json()).then(data => {
      if (data.success) {
        this.props.onSearchSubmit(data.data);
      } else {
        // TODO: handle error
      }
    });
  },
  render() {
    return (
      <div>
      <TextField
        hintText='Find music'
        value={this.state.q}
        onEnterKeyDown={this.handleSearch}
        onChange={this.handleSearchChange}/>
      <IconButton onTouchTap={this.handleSearch}>
        <SearchIcon />
      </IconButton>
    </div>
    )
  }
});
