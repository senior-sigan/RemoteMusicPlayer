import React from 'react';
import Track from './Track';
import List from 'material-ui/lib/lists/list';

export default React.createClass({
  render() {
    const list = this.props.data.map(track => {
      return (
        <Track key={track.id} title={track.title} artist={track.artist} url={track.url} />
      );
    });
    return (
      <List>
        {list}
      </List>
    )
  }
});