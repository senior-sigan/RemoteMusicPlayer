import React from 'react';

export default React.createClass({
  render() {
    return (
      <div className='track'>
        <span>{this.props.artist}</span>
        -
        <span>{this.props.title}</span>
      </div>
    )
  }
});
