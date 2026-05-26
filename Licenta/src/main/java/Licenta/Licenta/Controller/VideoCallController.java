package Licenta.Licenta.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller WebSocket pentru semnalizarea WebRTC (video call).
 *
 * Fluxul de semnalizare:
 *   1. Apelant  → /app/video-call.signal  (type: "call-offer")
 *   2. Server   → /user/{toUserId}/queue/video-call
 *   3. Destinatar răspunde cu "call-answer" sau "call-rejected"
 *   4. Ambele părți schimbă "ice-candidate" până se stabilește conexiunea P2P
 *   5. Oricare parte trimite "call-ended" pentru a încheia apelul
 *
 * Folosim Map<String, Object> în loc de DTO tipizat pentru a accepta
 * orice structură trimisă de frontend fără risc de deserializare eșuată.
 */
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
@RequiredArgsConstructor
@Controller
public class VideoCallController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/video-call.signal")
    public void handleVideoCallSignal(@Payload Map<String, Object> signal) {
        String toUserId = (String) signal.get("toUserId");
        String type = (String) signal.get("type");

        System.out.println("📹 Video call signal primit: type=" + type + " → toUserId=" + toUserId);

        if (toUserId == null || toUserId.isEmpty()) {
            System.out.println("⚠️ Signal ignorat — toUserId lipsă");
            return;
        }

        messagingTemplate.convertAndSendToUser(
                toUserId,
                "/queue/video-call",
                signal
        );

        System.out.println("✅ Signal trimis la user: " + toUserId);
    }
}
