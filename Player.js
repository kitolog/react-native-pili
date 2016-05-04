/**
 * Created by buhe on 16/5/4.
 */

var { requireNativeComponent, PropTypes, View } = require('react-native');

var iface = {
      propTypes: {
        ...View.propTypes,
      src: PropTypes.string,
    },
    };

module.exports = requireNativeComponent('RCTPlayer', iface);
