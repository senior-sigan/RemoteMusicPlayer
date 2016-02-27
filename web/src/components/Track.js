import React from 'react';
import 'whatwg-fetch';

export default React.createClass({
  handleClick(e) {
    fetch('/api/url.json', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      },
      body: JSON.stringify({
        url: this.props.url,
        title: this.props.title,
        artist: this.props.artist,
      }),
    });
  },
  render() {
    return (
      <div className='track' onClick={this.handleClick}>
        <span>{this.props.artist}</span>
        -
        <span>{this.props.title}</span>
      </div>
    )
  }
});
