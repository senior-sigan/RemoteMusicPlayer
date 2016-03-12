import React from 'react';
import Track from './Track';
import List from 'material-ui/lib/lists/list';

export default React.createClass({
  render() {
    const list = this.props.data.map(track => {
      return (
        <Track
          key={track.url}
          title={track.title}
          artist={track.artist}
          url={track.url}
          coverURL={track.coverURL}
          source={track.source}/>
      );
    });
    return (
      <List>
        {list}
      </List>
    )
  }
});
