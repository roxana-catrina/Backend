package Licenta.Licenta.Controller;

import Licenta.Licenta.Dto.MesajDTO;
import Licenta.Licenta.Service.MesajService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.Principal;
import java.util.Map;

@Controller
@CrossOrigin(origins = "http://localhost:4200")
public class WebSocketMesajController {

    @Autowired
    private MesajService mesajService;

    @MessageMapping("/chat.send")
    @SendToUser("/queue/messages")
    public MesajDTO sendMessage(@Payload MesajRequest request,
                                Principal principal) {
        return mesajService.trimiteMesaj(
                request.getExpeditorId(),
                request.getDestinatarId(),
                request.getContinut(),
                request.getTip(),
                request.getPacientId(),
                request.getPacientNume(),
                request.getPacientPrenume(),
                request.getPacientCnp(),
                request.getPacientDataNasterii(),
                request.getPacientSex(),
                request.getPacientNumarTelefon(),
                request.getPacientIstoricMedical(),
                request.getPacientDetalii(),
                request.getPacientNumarImagini()
        );
    }

    @MessageMapping("/chat.typing")
    @SendToUser("/queue/typing")
    public Map<String, Object> userTyping(@Payload Map<String, Object> payload) {
        return payload;
    }
}