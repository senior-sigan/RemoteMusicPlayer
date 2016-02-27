import React from 'react';
import Track from './Track';

export default React.createClass({
  render() {
    const list = this.props.data.map(track => {
      return (
        <Track key={track.id} title={track.title} artist={track.artist} url={track.url} />
      );
    });
    return (
      <div className='trackList'>
        {list}
      </div>
    )
  }
});
