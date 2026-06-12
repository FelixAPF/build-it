import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client'; //  Change to this

@Injectable({
  providedIn: 'root'
})
export class RealtimeService {
  private stompClient!: Client;
  private connectionReady$ = new Subject<boolean>();

  constructor() {
    this.initWebSocket();
  }

  private initWebSocket() {
    // Connect back to your Spring Boot /ws endpoint
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('STOMP Connected successfully.');
      this.connectionReady$.next(true);
    };

    this.stompClient.activate();
  }

  // Watch for incoming changes on a dynamic path
watchTopic(topicPath: string): Observable<any> {
  return new Observable((observer) => {
    let stompSubscription: any;

    const startSubscription = () => {
      stompSubscription = this.stompClient.subscribe(topicPath, (message) => {
        if (message.body) {
          observer.next(JSON.parse(message.body));
        }
      });
    };

    // Determine execution path based on connection state
    if (this.stompClient.connected) {
      startSubscription();
    } else {
      const connectedSub = this.connectionReady$.subscribe(() => {
        startSubscription();
        connectedSub.unsubscribe();
      });
    }

    // Unified Teardown: TypeScript is happy because every code path 
    // falls through to return this single cleanup function.
    return () => {
      if (stompSubscription) {
        stompSubscription.unsubscribe();
      }
    };
  });
}
}