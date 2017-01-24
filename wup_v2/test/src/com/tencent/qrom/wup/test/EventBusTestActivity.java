package com.tencent.qrom.wup.test;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.event.EventBus;
import qrom.component.wup.base.event.EventType;
import qrom.component.wup.base.event.IEventSubscriber;
import qrom.component.wup.base.event.Subscriber;
import android.app.Activity;
import android.os.Bundle;

public class EventBusTestActivity extends Activity implements IEventSubscriber {
	
	private static class TestEvent {
		private int value;
		
		public TestEvent(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}
	
	private SuperReceivor mReceivor = new SuperReceivor() {
		@Subscriber
		public void onTestEvent2(TestEvent event) {
			QRomLog.d("EventBusTestActivity", "mReceivor receive testEvent value=" + event.getValue());
		}

		@Override
		public IWorkRunner receiveEventOn(EventType eventType) {
			return null;
		}
		
		
	};
	
	private abstract class SuperReceivor implements IEventSubscriber {
		@Subscriber
		public void onTestEvent(TestEvent event) {
			QRomLog.d("EventBusTestActivity", "SuperReceivor receive testEvent value=" + event.getValue());
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		EventBus.getDefault().register(this);
		EventBus.getDefault().register(mReceivor);
		
		EventBus.getDefault().post(new TestEvent(10));
	}
	
	@Subscriber
	public void onTestEvent(TestEvent eventParam) {
		QRomLog.d("EventBusTestActivity", "receive testEvent value=" + eventParam.getValue());
	}
	
	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(mReceivor);
		EventBus.getDefault().unregister(this);
		
		super.onDestroy();
	}

	@Override
	public IWorkRunner receiveEventOn(EventType eventType) {
		return null;
	}

}
