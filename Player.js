/**
 * Created by buhe on 16/5/4.
 */

var { requireNativeComponent, PropTypes, View } = require('react-native');

var iface = {
      propTypes: {
        ...View.propTypes,
      source: PropTypes.object,
    },
    };

module.exports = requireNativeComponent('RCTPlayer', iface);
