import React from 'react';
import Track from './Track';

export default React.createClass({
  render() {
    const list = this.props.data.map(track => {
      return (
        <Track title={track.title} artist={track.artist} />
      );
    });
    return (
      <div className='trackList'>
        {list}
      </div>
    )
  }
});
