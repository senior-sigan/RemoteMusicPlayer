import React from 'react';
import { Link }  from 'react-router';

export default React.createClass({
  render() {
    return (
      <div>
        <header>
          <h1>Music room</h1>
        </header>
        <section>
          <form id='search' action='/api/vk.json' method='get'>
            <input name='q' type='search' placeholder='find music'></input>
            <input type='submit' value='Search'></input>
          </form>
        </section>
      </div>
    )
  }
});
