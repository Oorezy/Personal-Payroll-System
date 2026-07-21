package com.introtech.introtechutil.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class EmailNotificationVO {

    String email;

    String subject;

    String templateId;

    String attachmentName;

    byte[] attachment;

    Map<String, Object> variables;
}
