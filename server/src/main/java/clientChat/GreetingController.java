package clientChat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
@Slf4j
public class GreetingController {
    //  private Logger log;

    static Long id;
    @Autowired
    private SimpMessagingTemplate template;

    //при подписке на topic/greetings вызывает метод message
    @MessageMapping("/hello")
    public void gotMessage(Message message) throws Exception {
        System.out.println("Received " + message.getContent());
        System.out.println("Received Id" + message.getId());
        template.convertAndSend("/topic/greeting" + message.getId(), message);
    }


    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private UserRepository userRepository;
    @Autowired
    private EventsRepository eventRepository;


    //add user
    @GetMapping(path = "/add") // Map ONLY GET Requests
    public @ResponseBody
    Long addNewUser(@RequestParam String name
            , @RequestParam String email, @RequestParam String password) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        User n = new User();
        n.setName(name);
        n.setEmail(email);
        n.setPassword(password);
        for (User u : userRepository.findAll()) {
            if (u.getEmail().equals(email))
                return 0L;
        }
        userRepository.save(n);
        for (User u : userRepository.findAll()) {
            if (u.getEmail().equals(email))
                return u.getId();
        }
        return 0L;
    }

    @GetMapping(path = "/all")
    public @ResponseBody
    Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }

    @GetMapping(path = "/id")
    public @ResponseBody
    Optional<User> getByid() {
        // This returns a JSON or XML with the users
        return userRepository.findById(2L);

    }

    //create event
    @GetMapping(path = "/createEvent")
    public @ResponseBody
    String CreateEvent(@RequestParam Integer maxPeople, @RequestParam String name
            , @RequestParam String description, @RequestParam String place) {

        EventApp event = new EventApp();
        event.setMaxPeople(maxPeople);
        event.setDecription(description);
        event.setName(name);
        event.setPlace(place);
        eventRepository.save(event);
       /* for (User u : userRepository.findAll())
            template.convertAndSend("/topic/greeting" + u.getId(),
                    new Message("Message for" + u.getId()));*/

        template.convertAndSend("/topic/greeting/eventUpdate",
                new Message("Message for all"));

        // This returns a JSON or XML with the users
        return "eventCreated";

    }


    //добавить человека к событию
    @GetMapping(path = "/addToEvent")
    public @ResponseBody
    String addPerson() {

        eventRepository.findById(1L).get().addPeople("Vadim");
        eventRepository.findById(1L).get().addPeople("Ivan");
        // This returns a JSON or XML with the users
        return eventRepository.findById(1L).get().getPeople().get(0);

    }


    @GetMapping(path = "/idEvent")
    public @ResponseBody
    String getMessagesInEvent(@RequestParam Long id) {
        // This returns a JSON or XML with the users
        return eventRepository.findById(id).get().getMessage();
    }


}