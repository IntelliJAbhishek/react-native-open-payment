import { NativeModules, NativeEventEmitter } from "react-native";

const openPaymentEvents = new NativeEventEmitter(
  NativeModules.OpenPaymentEventEmitter
);

const removeSubscriptions = () => {
  openPaymentEvents.removeAllListeners("OpenPayment::PAYMENT_COMPLETED");
  openPaymentEvents.removeAllListeners("OpenPayment::PAYMENT_ERROR");
};

class OpenPayment {
  static startPayment(options, successCallback, errorCallback) {
    return new Promise(function (resolve, reject) {
      openPaymentEvents.addListener(
        "OpenPayment::PAYMENT_COMPLETED",
        (data) => {
          let resolveFn = successCallback || resolve;
          resolveFn(data);
          removeSubscriptions();
        }
      );
      openPaymentEvents.addListener("OpenPayment::PAYMENT_ERROR", (data) => {
        let rejectFn = errorCallback || reject;
        rejectFn(data);
        removeSubscriptions();
      });
      NativeModules.OpenPayment.startPayment(options);
    });
  }
}

export default OpenPayment;
