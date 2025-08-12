package com.usafe.guarantee.web;

import com.usafe.guarantee.domain.Guarantee;
import com.usafe.guarantee.service.GuaranteeService;
import com.usafe.guarantee.dto.GuaranteeForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class GuaranteeViewController {

    private final GuaranteeService service;

    @Value("${app.title}")
    private String appTitle;

    @Value("${app.version}")
    private String appVersion;

    @GetMapping({"/", "/guarantees", "/guarantees/list"})
    public String list(@RequestParam(required = false) String orderId,
                      @RequestParam(required = false) String buyerName,
                      Model model) {
        List<Guarantee> list;
        
        if (orderId != null && !orderId.trim().isEmpty() || 
            buyerName != null && !buyerName.trim().isEmpty()) {
            list = service.search(orderId, buyerName);
            model.addAttribute("searchOrderId", orderId);
            model.addAttribute("searchBuyerName", buyerName);
        } else {
            list = service.list();
        }
        
        model.addAttribute("title", "USAFE Guarantee – GitOps Delivery Demo");
        model.addAttribute("version", "0.0.3");
        model.addAttribute("guarantees", list);
        return "guarantees/list";  // /templates/guarantees/list.mustache
    }

    @GetMapping("/guarantees/new")
    public String newForm(Model model) {
        model.addAttribute("title", "USAFE Guarantee – GitOps Delivery Demo");
        model.addAttribute("version", "0.0.3");

        // 폼 초기값을 Map으로 내려서 Mustache가 100% 읽게 만든다
        java.util.Map<String, Object> emptyForm = new java.util.HashMap<>();
        emptyForm.put("orderId", "");
        emptyForm.put("buyerName", "");
        emptyForm.put("amount", "");

        model.addAttribute("form", emptyForm);
        return "guarantees/new";
    }




    @PostMapping("/guarantees")
    public String create(@Valid @ModelAttribute("form") GuaranteeForm form,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "USAFE Guarantee – GitOps Delivery Demo");
            model.addAttribute("version", "0.0.3");
            model.addAttribute("errors", bindingResult.getAllErrors()
                    .stream().map(e -> e.getDefaultMessage() == null ? "입력값을 확인하세요." : e.getDefaultMessage()).toList());
            return "guarantees/new";
        }
        Guarantee g = Guarantee.builder()
                .orderId(form.getOrderId())
                .buyerName(form.getBuyerName())
                .amount(form.getAmount())
                .build();
        service.issue(g);
        return "redirect:/guarantees/list";
    }

    @GetMapping("/guarantees/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Guarantee guarantee = service.get(id);
        
        model.addAttribute("title", "USAFE Guarantee – GitOps Delivery Demo");
        model.addAttribute("version", "0.0.3");
        model.addAttribute("guarantee", guarantee);
        
        // 폼 데이터 준비
        java.util.Map<String, Object> form = new java.util.HashMap<>();
        form.put("orderId", guarantee.getOrderId());
        form.put("buyerName", guarantee.getBuyerName());
        form.put("amount", guarantee.getAmount().toString());
        
        model.addAttribute("form", form);
        return "guarantees/edit";
    }

    @PostMapping("/guarantees/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("form") GuaranteeForm form,
                        BindingResult bindingResult,
                        Model model) {
        if (bindingResult.hasErrors()) {
            Guarantee guarantee = service.get(id);
            model.addAttribute("title", "USAFE Guarantee – GitOps Delivery Demo");
            model.addAttribute("version", "0.0.3");
            model.addAttribute("guarantee", guarantee);
            model.addAttribute("errors", bindingResult.getAllErrors()
                    .stream().map(e -> e.getDefaultMessage() == null ? "입력값을 확인하세요." : e.getDefaultMessage()).toList());
            return "guarantees/edit";
        }
        
        Guarantee updateRequest = Guarantee.builder()
                .orderId(form.getOrderId())
                .buyerName(form.getBuyerName())
                .amount(form.getAmount())
                .status(service.get(id).getStatus()) // 기존 상태 유지
                .build();
        
        service.update(id, updateRequest);
        return "redirect:/guarantees/list";
    }

    @PostMapping("/guarantees/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/guarantees/list";
    }

    @PostMapping("/guarantees/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        service.cancel(id);
        return "redirect:/guarantees/list";
    }

}
