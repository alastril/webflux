package com.hibernate.repository;

import com.hibernate.entity.Message;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("route")
public interface MessageRepository extends CrudRepository<Message, Long> {
}
