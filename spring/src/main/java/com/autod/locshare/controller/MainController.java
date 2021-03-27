package com.autod.locshare.controller;


import com.autod.locshare.dao.LocationRepository;
import com.autod.locshare.dao.UserRepository;
import com.autod.locshare.model.BrowseUser;
import com.autod.locshare.model.Location;
import com.autod.locshare.model.Request;
import com.autod.locshare.model.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Id;
import javax.servlet.http.HttpServletRequest;

@RestController
public class MainController {

    private LocationRepository locationRepository;
    private UserRepository userRepository;
    private HashMap<String, String> userNameMap = new HashMap<>();

    @Autowired
    public MainController(LocationRepository locationRepository, UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "/all", method = RequestMethod.POST)
    public Response<Collection<Location>> get(@RequestBody Request<String> request) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -10);
        List<Location> locations = null;
        if (request.getUser().getGroupId() == null || request.getUser().getGroupId()=="") {
            locations = locationRepository
                    .findAfterTime(new Timestamp(calendar.getTimeInMillis()), request.getUser().getId());

        }
        else {
            locations = locationRepository
                    .findAfterTime(new Timestamp(calendar.getTimeInMillis()), request.getUser().getGroupId(), request.getUser().getId());
        }


        HashMap<String, Location> distinctLocations = new HashMap<>();
        for (Location location : locations) {
            if (distinctLocations.containsKey(location.getUserId())) {
                Location another = distinctLocations.get(location.getUserId());
                if (another.getTime().before(location.getTime())) {
                    distinctLocations.replace(location.getUserId(), location);
                }
            }
            else {
                distinctLocations.put(location.getUserId(), location);
            }
        }

        for (Location location : distinctLocations.values()) {
            String userId = location.getUserId();
            if (userNameMap.containsKey(userId)) {
                location.setUserName(userNameMap.get(userId));
            }
            else {
                Optional<BrowseUser> userOrNull = userRepository.findById(userId);
                String name = null;
                if (userOrNull.isPresent()) {
                    name = userOrNull.get().getName();
                    if (name == null || name.equals("")) {
                        name = userId;
                    }

                }
                else {
                    //理论上不会到这里
                    name = userId;
                }
                userNameMap.put(userId, name);
                location.setUserName(name);
            }
        }
        return new Response.Builder<Collection<Location>>().withData(distinctLocations.values()).build();

    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Response<Void> update(@RequestBody Request<Location> request) {
        Location location = request.getData();
        location.setTime(new Timestamp(System.currentTimeMillis()));
        location.setUserId(request.getUser().getId());
        locationRepository.save(location);
        return new Response.Builder<Void>().build();
    }

    @PostMapping(value = "/registered")
    public Response<String> registered() {
        String id = UUID.randomUUID().toString();
        BrowseUser user = new BrowseUser();
        user.setId(id);
        userRepository.save(user);
        return new Response.Builder<String>().withData(id).build();
    }

    @RequestMapping(value = "/username", method = RequestMethod.POST)
    public Response<Boolean> changeUserName(@RequestBody Request<String> request, HttpServletRequest httpRequest) {

        Optional<BrowseUser> userOrNull = userRepository.findById(request.getUser().getId());
        if (userOrNull.isPresent()) {
            BrowseUser user = userOrNull.get();
            user.setName(request.getData());
            userRepository.save(user);
            return new Response.Builder<Boolean>().withData(true).build();
        }
        return new Response.Builder<Boolean>()
                .withData(false)
                .withCode(404)
                .withMessage("找不到账户")
                .build();
    }

    @RequestMapping(value = "/group", method = RequestMethod.POST)
    public Response<Boolean> changeGroup(@RequestBody Request<String> request, HttpServletRequest httpRequest) {

        Optional<BrowseUser> userOrNull = userRepository.findById(request.getUser().getId());
        if (userOrNull.isPresent()) {
            BrowseUser user = userOrNull.get();
            user.setGroupId(request.getData());
            userRepository.save(user);
            return new Response.Builder<Boolean>().withData(true).build();
        }
        return new Response.Builder<Boolean>()
                .withData(false)
                .withCode(404)
                .withMessage("找不到账户")
                .build();
    }

}
