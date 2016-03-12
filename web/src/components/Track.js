import React from 'react';
import 'whatwg-fetch';
import ListItem from 'material-ui/lib/lists/list-item';
import Snackbar from 'material-ui/lib/snackbar';
import Avatar from 'material-ui/lib/avatar';

export default React.createClass({
  getInitialState() {
    return {alert: false, message: ''};
  },
  handleClick(e) {
    fetch('/api/play.json', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      },
      body: JSON.stringify({
        url: this.props.url,
        title: this.props.title,
        artist: this.props.artist,
        source: this.props.source,
        coverURL: this.props.coverURL
      }),
    }).then(res => {
      this.setState({
        alert: true,
        message: `${this.props.artist} - ${this.props.title}`,
      });
    });
  },
  handleAlertClose() {
    this.setState({
      alert: false,
      message: '',
    });
  },
  render() {
    const avatar = (
      <Avatar src={this.props.coverURL || '/images/album-art.png'} />
    )
    return (
      <div>
        <ListItem
          leftAvatar={avatar}
          onTouchTap={this.handleClick}
          primaryText={this.props.artist}
          secondaryText={this.props.title} />
        <Snackbar
          open={this.state.alert}
          message={this.state.message}
          autoHideDuration={2000}
          onRequestClose={this.handleAlertClose}/>
      </div>
    )
  }
});
