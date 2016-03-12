import React from 'react';
import 'whatwg-fetch';
import ListItem from 'material-ui/lib/lists/list-item';
import Snackbar from 'material-ui/lib/snackbar';
import Avatar from 'material-ui/lib/avatar';

export default React.createClass({
  getInitialState() {
    this.searchCover(this.props.title, this.props.artist);
    return {alert: false, message: '', coverURL: this.props.coverURL};
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
        coverURL: this.state.coverURL
      }),
    }).then(res => {
      const newState = this.state;
      newState.alert = true;
      newState.message = `${this.props.artist} - ${this.props.title}`;
      this.setState(newState);
    });
  },
  handleAlertClose() {
    const newState = this.state;
    newState.alert = false;
    newState.message = '';
    this.setState(newState);
  },
  searchCover(title, artist) {
    const apiKey = '4143cc6c22d58d8ca468db3583c4e8b6';
    const url = `http://ws.audioscrobbler.com/2.0?method=track.getInfo&autocorrect=1&artist=${artist}&track=${title}&api_key=${apiKey}&format=json`;
    return fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json; charset=utf-8'
      },
    }).then(res => res.json()).then(data => {
      if (data && data.track && data.track.album && data.track.album.image && data.track.album.image[3] && data.track.album.image[3]['#text']) {
        const newState = this.state;
        newState.coverURL = data.track.album.image[3]['#text'];
        this.setState(newState);
      }
    });
  },
  render() {
    const avatar = (
      <Avatar src={this.state.coverURL || '/images/album-art.png'} />
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
