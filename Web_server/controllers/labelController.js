const Label = require('../models/labelModel')

exports.getAllLabels = (req, res) => {
    res.json(Label.getAllLabels())
}

exports.getLabelById = (req, res) => {
    const label = Label.getLabel(parseInt(req.params.id))
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
    const newLabel = Label.createLabel(name)
    res.status(201).location(`/api/labels/${newLabel.id}`).end()
}

exports.updateLabel = (req, res) => {
    const label = Label.getLabel(parseInt(req.params.id))
    if (!label) {
        return res.status(404).json({ error: 'Label not found' })
    }
    const { name } = req.body
    if(!name) {
        return res.status(400).json({ error: 'Name is required' })
    }
    const updated_label= Label.updateLabel(label.id, name)
    if (!updated_label) {
        return res.status(404).json({ error: 'Label not found' })
    }
    res.json(updated_label)
}

exports.deleteLabel = (req, res) => {
    const label = Label.getLabel(parseInt(req.params.id))
    if (!label) {
        return res.status(404).json({ error: 'Label not found' })
    }
    Label.deleteLabel(label.id)
    res.status(204).json()
}