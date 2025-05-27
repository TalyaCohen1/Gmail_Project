const Label = require('../models/labelModel')

exports.getAllLabels = (req, res) => {
    res.json(Label.getAllLabels(req.userId))
}

exports.getLabelById = (req, res) => {
    const label = Label.getLabel(parseInt(req.params.id), req.userId)
    if (!label) {
        return res.status(404).json({ error: 'Label not found' })
    }
    res.json(label)
}

exports.createLabel = (req, res) => {
    const { name } = req.body
    if (!name) {
        return res.status(400).json({ error: 'Name is required' })
    }
    const newLabel = Label.createLabel(name, req.userId)
    if (!newLabel) {
        return res.status(500).json({ error: 'Failed to create label' })
    }
    res.status(201).location(`/api/labels/${newLabel.id}`).end()
}

exports.updateLabel = (req, res) => {
    const label = Label.getLabel(parseInt(req.params.id), req.userId)
    if (!label) {
        return res.status(404).json({ error: 'Label not found' })
    }
    const { name } = req.body
    if(!name) {
        return res.status(400).json({ error: 'Name is required' })
    }
    const updated_label= Label.updateLabel(label.id, name, req.userId)
    if (!updated_label) {
        return res.status(404).json({ error: 'Label not found' })
    }
    res.json(updated_label)
}

exports.deleteLabel = (req, res) => {
    const label = Label.getLabel(parseInt(req.params.id), req.userId)
    if (!label) {
        return res.status(404).json({ error: 'Label not found' })
    }
    Label.deleteLabel(label.id, req.userId)
    res.status(204).json()
}