/**
 * Created by buhe on 16/4/29.
 */
//import React, {
//    View,
//    requireNativeComponent
//} from 'react-native';


//class Streaming extends React.Component {
//    render() {
//        return <RCTStreaming {...this.props} />;
//    }
//}
//
//Streaming.propTypes = {
//    ...View.propTypes,
//    streamProfile: React.PropTypes.string
//}
//
////RNChartView.propTypes = {
////
////    webLineWidth: React.PropTypes.number,
////    data: React.PropTypes.shape({
////        /**
////         * Coordinates for the center of the map.
////         */
////        x: React.PropTypes.array.isRequired,
////        y: React.PropTypes.array.isRequired
////    }),
////};
//
//var RCTStreaming = requireNativeComponent('RCTStreaming', Streaming);
//
//module.exports = Streaming;

//var React = require('react-native');
//var { requireNativeComponent } = React;
//var View = React.View;
//
//class RNChartView extends React.Component {
//    render() {
//        return <RCTChart
//            {...this.props}
//            />;
//    }
//}
//
//RNChartView.propTypes = {
//    ...View.propTypes,
//    streamProfile: React.PropTypes.bool,
//};
//
//var RCTChart = requireNativeComponent('RCTStreaming', RNChartView);
//
//module.exports = RNChartView;

var { requireNativeComponent, PropTypes, View } = require('react-native');

var iface = {
      propTypes: {
        ...View.propTypes,
      stream: PropTypes.object,
      muted: PropTypes.bool,
      zoom: PropTypes.number
    },
    };

module.exports = requireNativeComponent('RCTStreaming', iface);
